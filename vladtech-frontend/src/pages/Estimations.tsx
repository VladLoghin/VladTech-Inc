import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../components/card";
import { Button } from "../components/button";
import { Input } from "../components/input";
import { Label } from "../components/label";
import { Textarea } from "../components/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/select";
import { ArrowLeft, Plus, Trash2, Calculator, DollarSign, FileText } from "lucide-react";
import { Separator } from "../components/separator";
import { motion } from "motion/react";

interface LineItem {
  id: string;
  description: string;
  quantity: number;
  unitCost: number;
  total: number;
}

interface EstimationsProps {
  onNavigate: (page: string) => void;
}

export default function Estimations({ onNavigate }: EstimationsProps) {
  const [projectName, setProjectName] = useState("");
  const [clientName, setClientName] = useState("");
  const [projectType, setProjectType] = useState("");
  const [notes, setNotes] = useState("");
  const [lineItems, setLineItems] = useState<LineItem[]>([
    { id: "1", description: "", quantity: 1, unitCost: 0, total: 0 },
  ]);
  const [taxRate, setTaxRate] = useState(8.5);
  const [discount, setDiscount] = useState(0);

  const addLineItem = () => {
    setLineItems([
      ...lineItems,
      {
        id: Date.now().toString(),
        description: "",
        quantity: 1,
        unitCost: 0,
        total: 0,
      },
    ]);
  };

  const removeLineItem = (id: string) => {
    if (lineItems.length > 1) {
      setLineItems(lineItems.filter((item) => item.id !== id));
    }
  };

  const updateLineItem = (id: string, field: keyof LineItem, value: string | number) => {
    setLineItems(
      lineItems.map((item) => {
        if (item.id === id) {
          const updated = { ...item, [field]: value };
          if (field === "quantity" || field === "unitCost") {
            updated.total = Number(updated.quantity) * Number(updated.unitCost);
          }
          return updated;
        }
        return item;
      })
    );
  };

  const subtotal = lineItems.reduce((sum, item) => sum + item.total, 0);
  const discountAmount = (subtotal * discount) / 100;
  const taxableAmount = subtotal - discountAmount;
  const taxAmount = (taxableAmount * taxRate) / 100;
  const grandTotal = taxableAmount + taxAmount;

  const handleSaveEstimate = () => {
    const estimate = {
      projectName,
      clientName,
      projectType,
      notes,
      lineItems,
      subtotal,
      discount,
      discountAmount,
      taxRate,
      taxAmount,
      grandTotal,
      createdAt: new Date().toISOString(),
    };
    console.log("Estimate saved:", estimate);
    alert("Estimate saved successfully! Check console for details.");
  };

  const handleClear = () => {
    setProjectName("");
    setClientName("");
    setProjectType("");
    setNotes("");
    setLineItems([{ id: "1", description: "", quantity: 1, unitCost: 0, total: 0 }]);
    setTaxRate(8.5);
    setDiscount(0);
  };

  return (
    <div className="min-h-screen bg-black">
      {/* Header - Liquid Glass Style */}
      <div className="bg-black/60 backdrop-blur-xl border-b border-yellow-400/20 shadow-2xl sticky top-0 z-40">
        <div className="container mx-auto px-6 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button
                onClick={() => onNavigate("home")}
                variant="ghost"
                className="text-white hover:text-yellow-400 hover:bg-white/10"
              >
                <ArrowLeft className="mr-2 h-5 w-5" />
                BACK TO HOME
              </Button>
              <h1 className="text-3xl text-white tracking-wide">PROJECT ESTIMATIONS</h1>
            </div>
            <div className="flex gap-3">
              <Button
                onClick={handleClear}
                variant="outline"
                className="bg-transparent text-white border-yellow-400/40 hover:bg-yellow-400/10 hover:border-yellow-400"
              >
                CLEAR ALL
              </Button>
              <Button
                onClick={handleSaveEstimate}
                className="bg-yellow-400 text-black hover:bg-yellow-500"
              >
                <FileText className="mr-2 h-5 w-5" />
                SAVE ESTIMATE
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container mx-auto px-6 py-8">
        <div className="grid lg:grid-cols-3 gap-8">
          {/* Left Column - Project Details - Glassmorphism */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="lg:col-span-2"
          >
            <div className="relative group">
              <div className="absolute -inset-1 bg-gradient-to-r from-yellow-400/20 to-transparent rounded-3xl blur opacity-50"></div>
              <Card className="relative bg-white/5 backdrop-blur-xl border-yellow-400/20 shadow-2xl">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2 text-2xl text-white tracking-wide">
                    <Calculator className="h-6 w-6 text-yellow-400" />
                    ESTIMATE DETAILS
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-6">
                  {/* Project Information */}
                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="projectName" className="text-white/70">Project Name</Label>
                      <Input
                        id="projectName"
                        value={projectName}
                        onChange={(e) => setProjectName(e.target.value)}
                        placeholder="Enter project name"
                        className="bg-white/5 border-yellow-400/20 text-white placeholder:text-white/30"
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="clientName" className="text-white/70">Client Name</Label>
                      <Input
                        id="clientName"
                        value={clientName}
                        onChange={(e) => setClientName(e.target.value)}
                        placeholder="Enter client name"
                        className="bg-white/5 border-yellow-400/20 text-white placeholder:text-white/30"
                      />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="projectType" className="text-white/70">Project Type</Label>
                    <Select value={projectType} onValueChange={setProjectType}>
                      <SelectTrigger className="bg-white/5 border-yellow-400/20 text-white">
                        <SelectValue placeholder="Select project type" />
                      </SelectTrigger>
                      <SelectContent className="bg-black border-yellow-400/30">
                        <SelectItem value="construction" className="text-white focus:bg-yellow-400/20 focus:text-white">Construction</SelectItem>
                        <SelectItem value="renovation" className="text-white focus:bg-yellow-400/20 focus:text-white">Renovation</SelectItem>
                        <SelectItem value="engineering" className="text-white focus:bg-yellow-400/20 focus:text-white">Engineering</SelectItem>
                        <SelectItem value="technology" className="text-white focus:bg-yellow-400/20 focus:text-white">Technology Integration</SelectItem>
                        <SelectItem value="consulting" className="text-white focus:bg-yellow-400/20 focus:text-white">Consulting</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <Separator className="bg-yellow-400/20" />

                  {/* Line Items */}
                  <div>
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-xl text-white tracking-wide">LINE ITEMS</h3>
                      <Button
                        onClick={addLineItem}
                        size="sm"
                        className="bg-yellow-400 text-black hover:bg-yellow-500"
                      >
                        <Plus className="mr-2 h-4 w-4" />
                        ADD ITEM
                      </Button>
                    </div>

                    <div className="space-y-4">
                      {lineItems.map((item, index) => (
                        <div
                          key={item.id}
                          className="grid grid-cols-12 gap-3 items-end p-4 bg-black/40 backdrop-blur-sm border border-yellow-400/20 rounded-lg"
                        >
                          <div className="col-span-5">
                            <Label htmlFor={`desc-${item.id}`} className="text-sm text-white/70">
                              Description
                            </Label>
                            <Input
                              id={`desc-${item.id}`}
                              value={item.description}
                              onChange={(e) =>
                                updateLineItem(item.id, "description", e.target.value)
                              }
                              placeholder="Item description"
                              className="bg-white/5 border-yellow-400/20 text-white placeholder:text-white/30"
                            />
                          </div>
                          <div className="col-span-2">
                            <Label htmlFor={`qty-${item.id}`} className="text-sm text-white/70">
                              Quantity
                            </Label>
                            <Input
                              id={`qty-${item.id}`}
                              type="number"
                              min="0"
                              value={item.quantity}
                              onChange={(e) =>
                                updateLineItem(item.id, "quantity", Number(e.target.value))
                              }
                              className="bg-white/5 border-yellow-400/20 text-white"
                            />
                          </div>
                          <div className="col-span-2">
                            <Label htmlFor={`cost-${item.id}`} className="text-sm text-white/70">
                              Unit Cost ($)
                            </Label>
                            <Input
                              id={`cost-${item.id}`}
                              type="number"
                              min="0"
                              step="0.01"
                              value={item.unitCost}
                              onChange={(e) =>
                                updateLineItem(item.id, "unitCost", Number(e.target.value))
                              }
                              className="bg-white/5 border-yellow-400/20 text-white"
                            />
                          </div>
                          <div className="col-span-2">
                            <Label className="text-sm text-white/70">Total</Label>
                            <div className="h-10 px-3 py-2 bg-yellow-400/20 border border-yellow-400/30 rounded-md flex items-center text-yellow-400">
                              ${item.total.toFixed(2)}
                            </div>
                          </div>
                          <div className="col-span-1">
                            <Button
                              onClick={() => removeLineItem(item.id)}
                              variant="ghost"
                              size="sm"
                              className="text-red-400 hover:text-red-300 hover:bg-red-400/10"
                              disabled={lineItems.length === 1}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  <Separator className="bg-yellow-400/20" />

                  {/* Notes */}
                  <div className="space-y-2">
                    <Label htmlFor="notes" className="text-white/70">Additional Notes</Label>
                    <Textarea
                      id="notes"
                      value={notes}
                      onChange={(e) => setNotes(e.target.value)}
                      placeholder="Add any additional notes or terms..."
                      className="min-h-[100px] bg-white/5 border-yellow-400/20 text-white placeholder:text-white/30"
                    />
                  </div>
                </CardContent>
              </Card>
            </div>
          </motion.div>

          {/* Right Column - Summary - Glassmorphism */}
          <div className="space-y-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
            >
              <div className="relative group">
                <div className="absolute -inset-1 bg-gradient-to-r from-yellow-400/20 to-transparent rounded-3xl blur opacity-50"></div>
                <Card className="relative bg-white/5 backdrop-blur-xl border-yellow-400/20 shadow-2xl">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-xl text-white tracking-wide">
                      <DollarSign className="h-5 w-5 text-yellow-400" />
                      SUMMARY
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-300">Subtotal:</span>
                      <span className="text-lg text-white">${subtotal.toFixed(2)}</span>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="discount" className="text-white/70">Discount (%)</Label>
                      <Input
                        id="discount"
                        type="number"
                        min="0"
                        max="100"
                        step="0.1"
                        value={discount}
                        onChange={(e) => setDiscount(Number(e.target.value))}
                        className="bg-white/5 border-yellow-400/20 text-white"
                      />
                    </div>

                    {discount > 0 && (
                      <div className="flex justify-between items-center text-green-400">
                        <span>Discount Amount:</span>
                        <span>-${discountAmount.toFixed(2)}</span>
                      </div>
                    )}

                    <Separator className="bg-yellow-400/20" />

                    <div className="space-y-2">
                      <Label htmlFor="taxRate" className="text-white/70">Tax Rate (%)</Label>
                      <Input
                        id="taxRate"
                        type="number"
                        min="0"
                        max="100"
                        step="0.1"
                        value={taxRate}
                        onChange={(e) => setTaxRate(Number(e.target.value))}
                        className="bg-white/5 border-yellow-400/20 text-white"
                      />
                    </div>

                    <div className="flex justify-between items-center text-gray-300">
                      <span>Tax Amount:</span>
                      <span>${taxAmount.toFixed(2)}</span>
                    </div>

                    <Separator className="bg-yellow-400/20" />

                    <div className="flex justify-between items-center p-4 bg-yellow-400 rounded-lg">
                      <span className="text-xl tracking-wide text-black">GRAND TOTAL:</span>
                      <span className="text-2xl text-black">
                        ${grandTotal.toFixed(2)}
                      </span>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
            >
              <div className="relative group">
                <div className="absolute -inset-1 bg-gradient-to-r from-yellow-400/20 to-transparent rounded-3xl blur opacity-50"></div>
                <Card className="relative bg-white/5 backdrop-blur-xl border-yellow-400/20 shadow-2xl">
                  <CardContent className="pt-6">
                    <h4 className="mb-3 text-white tracking-wide">QUICK TIPS</h4>
                    <ul className="space-y-2 text-sm text-gray-300">
                      <li>• Add multiple line items for detailed estimates</li>
                      <li>• Apply discounts for loyal clients</li>
                      <li>• Adjust tax rate based on location</li>
                      <li>• Save estimates for client records</li>
                    </ul>
                  </CardContent>
                </Card>
              </div>
            </motion.div>
          </div>
        </div>
      </div>
    </div>
  );
}
